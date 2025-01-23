# Antora GitHub Pages Demo

This shows how to use GitHub Actions to build and publish GitHub Pages using [Antora](https://antora.org).

Includes a [pipeline](.github/workflows/pages.yml) that runs the required Actions to publish a GitHub Pages instance.
The pipeline has been inspired by
the [Pages starter workflows](https://github.com/actions/starter-workflows/tree/main/pages).

Not mentioned in the [actions/configure-pages](https://github.com/actions/configure-pages) action is the requirement
to [correctly configure](https://stackoverflow.com/a/73967433/2920585) the repository or else the action will fail:
`Settings > Pages > Build and deployment` select `Source: GitHub Actions`

Additional workflows (beyond the magic `pages.yml`) are offered:

- `pages-gh-cli.yml` (Deploy Pages via GH CLI): uses `actions/upload-artifact@v4` and does a three tiered manual build,
  configure, and deploy of Pages with everything explicitly handled in the workflows. The magic
  `actions/upload-pages-artifact` and `actions/deploy-pages` actions are not used.
- `pages-gh-cli-setup.yml` (Setup Pages via GH CLI) and `pages-gh-cli-deploy.yml` (Publish Pages via GH CLI): similar to
  the other mentioned option, however, while also usin the modern (> `v4`) version of `actions/upload-artifact` was
  originally built with the
  [v3 limitations](https://github.blog/changelog/2023-12-14-github-actions-artifacts-v4-is-now-generally-available/) in
  mind. See the workflow files for additional details.
