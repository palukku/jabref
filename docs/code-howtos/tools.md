---
parent: Code Howtos
---
# Useful development tooling

This page lists some software we consider useful.

## Browser plugins

* [Refined GitHub](https://github.com/sindresorhus/refined-github) - GitHub on steroids
* [GitHub Issue Link Status](https://github.com/fregante/github-issue-link-status) - proper coloring of linked issues and PRs.
* [Codecov Browser Extension](https://github.com/codecov/browser-extension) - displaying code coverage directly when browsing GitHub
* [Sourcegraph Browser Extension](https://sourcegraph.com/docs/integration/browser_extension) - Navigate through source on GitHub

## git hints

Here, we collect some helpful git hints

* <https://github.com/blog/2019-how-to-undo-almost-anything-with-git>
* [So you need to change your commit](https://github.com/RichardLitt/knowledge/blob/master/github/amending-a-commit-guide.md#so-you-need-to-change-your-commit)
* awesome hints and tools regarding git: <https://github.com/dictcp/awesome-git>

### Rebase everything as one commit on main

* Precondition: `JabRef/jabref` is [configured as upstream](https://help.github.com/articles/configuring-a-remote-for-a-fork/).
* Fetch recent commits and prune non-existing branches: `git fetch upstream --prune`
* Merge recent commits: `git merge upstream/main`
* If there are conflicts, resolve them
* Reset index to upstream/main: `git reset upstream/main`
* Review the changes and create a new commit using git gui: `git gui`
* Do a force push: `git push -f origin`

See also: [https://help.github.com/articles/syncing-a-fork/](https://help.github.com/articles/syncing-a-fork/)

## Tooling for Windows

(As Administrator - one time)

1. Install [chocolatey](https://chocolatey.org)
2. `choco install git.install -y --params "/GitAndUnixToolsOnPath /WindowsTerminal"`
3. `choco install notepadplusplus`
4. If you want to have your JDK also managed via chocolatey: `choco install temurin`

Then, each weak do `choco upgrade all` to ensure all tooling is kept updated.

### General git tooling on Windows

* Use [git for windows](https://git-for-windows.github.io), no additional git tooling required
  * [Git Credential Manager for Windows](https://github.com/Microsoft/Git-Credential-Manager-for-Windows) is included. Ensure that you include that in the installation. Aim: Store password for GitHub permanently for `https` repository locations
* [Use notepad++ as editor](http://stackoverflow.com/a/2486342/873282) for `git rebase -i`

### Better console applications

#### ConEmu plus clink

* `choco install conemu clink`
* [ConEmu](http://conemu.github.io) -> Preview Version - Aim: Colorful console with tabs
  * At first start:
    * "Choose your startup task ...": \`{Bash::Git bash\}}
    * `OK`
    * Upper right corner: "Settings..." (third entrry Eintrag)
    * Startup/Tasks: Choose task no. 7 ("Bash::Git bash"). At "Task parameters" `/dir C:\git-repositories\jabref\jabref`
    * `Save Settings`
* [clink](http://mridgers.github.io/clink/) - Aim: Unix keys (<kbd>Alt</kbd>+<kbd>B</kbd>, <kbd>Ctrl</kbd>+<kbd>S</kbd>, etc.) also available at the prompt of `cmd.exe`

#### Other bundles

* [Cmder](https://cmder.app/) - bundles ConEmu plus clink

### Tools for working with XMP

* Validate XMP: <https://www.pdflib.com/pdf-knowledge-base/xmp/free-xmp-validator>
