#!/usr/bin/env python3
"""
HoverDict 词典一键更新工具

自动完成：下载 ECDICT → 转换格式 → 导入项目

使用方法（在项目根目录运行）：
    python3 tools/convert_ecdict.py

可选参数：
    --max N       限制最大词条数（默认不限制，全量约 20 万+）
    --output PATH 自定义输出路径（默认自动放到项目 resources 目录）
    --keep-csv    保留下载的 ecdict.csv 不删除
"""

import csv
import sys
import os
import re
import zipfile
import urllib.request
import urllib.error
import tempfile
import shutil

# ============================================================
# 配置
# ============================================================
ECDICT_CSV_URL = "https://raw.githubusercontent.com/skywind3000/ECDICT/master/ecdict.csv"
ECDICT_ZIP_URL = "https://github.com/skywind3000/ECDICT/archive/refs/heads/master.zip"
ECDICT_CSV_IN_ZIP = "ECDICT-master/ecdict.csv"

# 项目内词典的相对路径
DEFAULT_OUTPUT_REL = os.path.join("src", "main", "resources", "dictionary", "en_zh.dict")


# ============================================================
# 下载
# ============================================================
def download_ecdict_csv(dest_dir: str) -> str:
    """下载 ECDICT 的 ecdict.csv，返回本地文件路径"""
    csv_path = os.path.join(dest_dir, "ecdict.csv")

    # 方式 1：直接下载 CSV
    print("📥 尝试直接下载 ecdict.csv ...")
    try:
        req = urllib.request.Request(ECDICT_CSV_URL, headers={"User-Agent": "HoverDict/1.0"})
        with urllib.request.urlopen(req, timeout=120) as resp:
            total = int(resp.headers.get("Content-Length", 0))
            downloaded = 0
            with open(csv_path, "wb") as f:
                while True:
                    chunk = resp.read(1024 * 256)
                    if not chunk:
                        break
                    f.write(chunk)
                    downloaded += len(chunk)
                    if total > 0:
                        pct = downloaded * 100 // total
                        print(f"\r   下载中... {downloaded // 1024 // 1024}MB / {total // 1024 // 1024}MB ({pct}%)", end="", flush=True)
                    else:
                        print(f"\r   下载中... {downloaded // 1024 // 1024}MB", end="", flush=True)
            print()
            print(f"✅ 下载完成: {os.path.getsize(csv_path) // 1024 // 1024}MB")
            return csv_path
    except (urllib.error.URLError, urllib.error.HTTPError, OSError) as e:
        print(f"\n⚠️  直接下载失败: {e}")

    # 方式 2：下载 ZIP 解压
    print("📥 尝试下载 ZIP 包 ...")
    try:
        zip_path = os.path.join(dest_dir, "ecdict.zip")
        req = urllib.request.Request(ECDICT_ZIP_URL, headers={"User-Agent": "HoverDict/1.0"})
        with urllib.request.urlopen(req, timeout=180) as resp:
            downloaded = 0
            with open(zip_path, "wb") as f:
                while True:
                    chunk = resp.read(1024 * 256)
                    if not chunk:
                        break
                    f.write(chunk)
                    downloaded += len(chunk)
                    print(f"\r   下载中... {downloaded // 1024 // 1024}MB", end="", flush=True)
        print()
        print("📦 解压 ecdict.csv ...")
        with zipfile.ZipFile(zip_path, "r") as zf:
            with zf.open(ECDICT_CSV_IN_ZIP) as src, open(csv_path, "wb") as dst:
                shutil.copyfileobj(src, dst)
        os.remove(zip_path)
        print(f"✅ 解压完成: {os.path.getsize(csv_path) // 1024 // 1024}MB")
        return csv_path
    except Exception as e:
        print(f"\n❌ ZIP 下载也失败: {e}")

    print()
    print("=" * 60)
    print("自动下载失败，请手动操作：")
    print("  1. 打开 https://github.com/skywind3000/ECDICT")
    print("  2. 下载 ecdict.csv")
    print(f"  3. 放到: {dest_dir}/ecdict.csv")
    print("  4. 重新运行本脚本")
    print("=" * 60)
    sys.exit(1)


# ============================================================
# 转换
# ============================================================
def clean_translation(translation: str) -> str:
    if not translation:
        return ""
    lines = [l.strip() for l in translation.replace("\\n", "\n").split("\n") if l.strip()]
    results = []
    for line in lines:
        if line.startswith("[网络]"):
            continue
        cleaned = re.sub(r"^[a-z]+\.\s*", "", line).strip()
        if cleaned:
            results.append(cleaned)
    if not results:
        return ""
    combined = "；".join(results)
    if len(combined) > 100:
        combined = combined[:100] + "…"
    return combined


def is_useful_word(word: str) -> bool:
    if not word or len(word) < 1:
        return False
    if word.isdigit():
        return False
    if word.startswith("-") or word.startswith("'"):
        return False
    if word.count(" ") > 2:
        return False
    if any(c in word for c in ["(", ")", "/", '"', "#"]):
        return False
    return True


def convert_ecdict(input_csv: str, output_dict: str, max_entries: int = 0) -> int:
    print(f"\n🔄 转换中...")
    entries = {}
    skipped = 0
    total = 0

    with open(input_csv, "r", encoding="utf-8") as f:
        reader = csv.reader(f)
        header = next(reader)
        try:
            word_idx = header.index("word")
            trans_idx = header.index("translation")
        except ValueError as e:
            print(f"❌ CSV 格式不对: {e}")
            sys.exit(1)

        for row in reader:
            total += 1
            if total % 100000 == 0:
                print(f"   已处理 {total} 条...")
            if len(row) <= max(word_idx, trans_idx):
                skipped += 1
                continue
            word = row[word_idx].strip()
            translation = row[trans_idx].strip() if trans_idx < len(row) else ""
            if not is_useful_word(word):
                skipped += 1
                continue
            cleaned = clean_translation(translation)
            if not cleaned:
                skipped += 1
                continue
            key = word.strip().lower()
            if key not in entries or len(cleaned) < len(entries[key]):
                entries[key] = cleaned

    print(f"   总行数: {total}")
    print(f"   跳过: {skipped}")
    print(f"   有效词条: {len(entries)}")

    if max_entries > 0 and len(entries) > max_entries:
        print(f"   限制为 {max_entries} 条")
        sorted_keys = sorted(entries.keys())[:max_entries]
        entries = {k: entries[k] for k in sorted_keys}

    os.makedirs(os.path.dirname(output_dict) or ".", exist_ok=True)
    with open(output_dict, "w", encoding="utf-8") as f:
        for word in sorted(entries.keys()):
            f.write(f"{word}\t{entries[word]}\n")

    file_size = os.path.getsize(output_dict)
    print(f"\n✅ 词典生成完成!")
    print(f"   路径: {output_dict}")
    print(f"   词条: {len(entries)}")
    print(f"   大小: {file_size / 1024 / 1024:.1f} MB")
    return len(entries)


# ============================================================
# 主流程
# ============================================================
def find_project_root() -> str:
    for start in [os.path.dirname(os.path.abspath(__file__)), os.path.abspath(os.getcwd())]:
        check = start
        for _ in range(5):
            if os.path.exists(os.path.join(check, "build.gradle.kts")):
                return check
            parent = os.path.dirname(check)
            if parent == check:
                break
            check = parent
    return os.getcwd()


def main():
    max_entries = 0
    output_path = None
    keep_csv = False

    i = 1
    while i < len(sys.argv):
        arg = sys.argv[i]
        if arg == "--max" and i + 1 < len(sys.argv):
            max_entries = int(sys.argv[i + 1])
            i += 2
        elif arg == "--output" and i + 1 < len(sys.argv):
            output_path = sys.argv[i + 1]
            i += 2
        elif arg == "--keep-csv":
            keep_csv = True
            i += 1
        elif arg in ("-h", "--help"):
            print(__doc__)
            sys.exit(0)
        else:
            print(f"未知参数: {arg}")
            sys.exit(1)

    project_root = find_project_root()
    if output_path is None:
        output_path = os.path.join(project_root, DEFAULT_OUTPUT_REL)

    print("=" * 60)
    print("  HoverDict 词典一键更新工具")
    print("=" * 60)
    print(f"  项目目录: {project_root}")
    print(f"  输出路径: {output_path}")
    if max_entries > 0:
        print(f"  词条上限: {max_entries}")
    print()

    # Step 1: 下载
    tmp_dir = tempfile.mkdtemp(prefix="hoverdict_")
    try:
        csv_path = download_ecdict_csv(tmp_dir)

        # Step 2: 转换 + 导入
        count = convert_ecdict(csv_path, output_path, max_entries)

        # Step 3: 可选保留 CSV
        if keep_csv:
            final_csv = os.path.join(project_root, "ecdict.csv")
            shutil.copy2(csv_path, final_csv)
            print(f"\n📁 CSV 已保留: {final_csv}")
    finally:
        shutil.rmtree(tmp_dir, ignore_errors=True)

    print()
    print("=" * 60)
    print(f"  🎉 完成! 词条数: 970 → {count}")
    print()
    print(f"  下一步: gradle buildPlugin")
    print("=" * 60)


if __name__ == "__main__":
    main()
