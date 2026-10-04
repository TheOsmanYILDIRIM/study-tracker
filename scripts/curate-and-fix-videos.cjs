#!/usr/bin/env node
/**
 * scripts/curate-and-fix-videos.cjs
 * Curates topic-specific videos with verified subtitles for suspicious duplicates and broken links.
 */
const fs = require("fs");
const path = require("path");
const cp = require("child_process");
const crypto = require("crypto");

const ROOT = path.resolve(__dirname, "..");
const ITEMS = path.join(ROOT, "content", "v2", "items");
const TRANSCRIPTS = path.join(ROOT, "content", "v2", "transcripts");
fs.mkdirSync(TRANSCRIPTS, { recursive: true });

function readJson(p) { return JSON.parse(fs.readFileSync(p, "utf8")); }
function writeJson(p, o) { fs.writeFileSync(p, JSON.stringify(o, null, 2) + "\n"); }
function sha16(s) { return crypto.createHash("sha256").update(s).digest("hex").slice(0, 16); }

function cleanVtt(text) {
  return String(text)
    .replace(/^WEBVTT.*$/gmi, "")
    .replace(/^Kind:.*$/gmi, "")
    .replace(/^Language:.*$/gmi, "")
    .replace(/^\d+$/gm, "")
    .replace(/^\d{2}:\d{2}:\d{2}\.\d{3}\s+-->.*$/gm, "")
    .replace(/<[^>]+>/g, "")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .split(/\r?\n/)
    .map(x => x.trim())
    .filter(Boolean)
    .filter((x, i, a) => i === 0 || x !== a[i - 1])
    .join("\n")
    .trim();
}

function listSubtitleFiles(prefix) {
  return fs.readdirSync(TRANSCRIPTS)
    .filter(f => f.startsWith(prefix + ".") && /\.vtt$/i.test(f))
    .map(f => path.join(TRANSCRIPTS, f));
}

function chooseSubtitle(files) {
  const score = f => {
    const n = path.basename(f).toLowerCase();
    let s = 0;
    if (/\.tr\.vtt$/.test(n)) s += 10;
    if (/\.tr-[a-z0-9-]+\.vtt$/.test(n)) s += 8;
    if (/\.vtt$/.test(n)) s += 1;
    return s;
  };
  return [...files].sort((a, b) => score(b) - score(a))[0] || null;
}

function fetchTranscriptForUrl(itemId, url) {
  const outPrefix = itemId;
  const tmpl = path.join(TRANSCRIPTS, outPrefix + ".%(language)s.%(ext)s");
  for (const old of listSubtitleFiles(outPrefix)) {
    try { fs.unlinkSync(old); } catch {}
  }

  const args = [
    "--skip-download",
    "--write-subs",
    "--write-auto-subs",
    "--sub-langs", "tr,tr-*",
    "--sub-format", "vtt",
    "--no-playlist",
    "--quiet",
    "--no-warnings",
    "-o", tmpl,
    url
  ];

  try {
    cp.execFileSync("yt-dlp", args, { stdio: ["ignore", "pipe", "pipe"], timeout: 60000, encoding: "utf8" });
    const subtitle = chooseSubtitle(listSubtitleFiles(outPrefix));
    if (!subtitle) return null;
    const raw = fs.readFileSync(subtitle, "utf8");
    const normalized = cleanVtt(raw);
    if (!normalized) return null;
    const txtPath = path.join(TRANSCRIPTS, outPrefix + ".tr.txt");
    fs.writeFileSync(txtPath, normalized + "\n");
    const kind = /auto|orig|asr/i.test(path.basename(subtitle)) ? "auto" : "subtitle";
    return {
      transcriptPath: path.relative(ROOT, txtPath),
      transcriptRawPath: path.relative(ROOT, subtitle),
      transcriptFingerprint: sha16(normalized),
      transcriptLanguage: "tr",
      transcriptKind: kind
    };
  } catch (e) {
    return null;
  }
}

function searchYouTubeCandidates(query, limit = 5) {
  try {
    const out = cp.execFileSync("yt-dlp", [
      `ytsearch${limit}:${query}`,
      "--print", "%(id)s\t%(title)s\t%(channel)s",
      "--no-warnings"
    ], { timeout: 45000, encoding: "utf8" });

    return out.split(/\r?\n/)
      .map(line => line.trim())
      .filter(Boolean)
      .map(line => {
        const [id, title, channel] = line.split("\t");
        return {
          id,
          url: `https://www.youtube.com/watch?v=${id}`,
          title: title || "",
          channel: channel || ""
        };
      });
  } catch (e) {
    return [];
  }
}

module.exports = {
  readJson,
  writeJson,
  sha16,
  fetchTranscriptForUrl,
  searchYouTubeCandidates,
  ITEMS,
  ROOT
};
