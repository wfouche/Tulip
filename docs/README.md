# Antora GitHub Pages Demo

This shows how to use GitHub Actions to build and publish GitHub Pages using [Antora](https://antora.org).

Includes a [pipeline](.github/workflows/pages.yml) that runs the required Actions to publish a GitHub Pages instance.
The pipeline has been inspired by
the [Pages starter workflows](https://github.com/actions/starter-workflows/tree/main/pages).

Not mentioned in the [actions/configure-pages](https://github.com/actions/configure-pages) action is the requirement
to [correctly configure](https://stackoverflow.com/a/73967433/2920585) the repository or else the action will fail:
`Settings > Pages > Build and deployment` select `Source: GitHub Actions`
