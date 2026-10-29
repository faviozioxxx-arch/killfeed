# KillFeed
Paper 1.21.11 плагин: панели при убийствах, банах, киках и т.д.

## Сборка через GitHub
1. Залей проект в репозиторий (ветка `main`).
2. Вкладка **Actions** -> **Build** -> дождись зелёной галочки.
3. Внутри запуска, раздел **Artifacts** -> скачай `KillFeed` (там jar).
4. Jar положи в `plugins/`.

Релиз с jar: создай тег `git tag v1.0.0 && git push --tags`, jar появится в **Releases**.

## Локальная сборка
`gradle build` (Java 21), jar будет в `build/libs/`.
