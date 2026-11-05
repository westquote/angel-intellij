<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# angel-intellij Changelog

## [Unreleased]

## [0.3.1-gs] - 2026-11-04

- Colour .as files from the lexer: keywords, comments, strings, numbers, identifiers, operators and punctuation, editable under Settings > Editor > Color Scheme > AngelScript (previously every token returned no highlight)
- Fix the bundled language server crashing on every edit with `Notification handler 'textDocument/didChange' failed with message: vN is not a function` (namespace imports of `node:assert` are not callable once esbuild bundles them)

## [0.3.0] - 2026-09-22

- update language server
- update plugin id

## [0.2.0] - 2026-09-19

- expose language server settings

## [0.1.1] - 2025-04-09

- Update LSP version
- Support for bracket matching

## [0.1.0] 2025/02/24

- Initial pre-release
