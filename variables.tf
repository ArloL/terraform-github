variable "repository" {
    description = "Map of repository names"
    type = map(any)
    default = {
        abenteuer-irland = {
            name = "abenteuer-irland"
            description = "Mum's website for Abenteuer Irland."
            main_branch = "gh-pages"
            homepage = "https://arlol.github.io/abenteuer-irland/"
        }
        "arlol.github.io" = {
            name = "arlol.github.io"
            description = ""
        }
        business-english = {
            name = "business-english"
            description = "Mum's website for Business English."
            main_branch = "gh-pages"
        }
        chorito = {
            name = "chorito"
            description = "A tool that does some chores in your source code."
        }
        corporate-python = {
            name = "corporate-python"
            description = ""
        }
        dotfiles = {
            name = "dotfiles"
            description = "My collection of dotfiles used to configure my command line environments."
        }
        eclipse-projects = {
            name = "eclipse-projects"
            description = "Arlo's project catalog for the Eclipse Installer"
            homepage = "https://arlol.github.io/eclipse-projects/"
        }
        gitfx = {
            name = "gitfx"
            description = "A simple git client with Java 11+, JavaFX 15+ and GraalVM."
        }
        homebrew-tap = {
            name = "homebrew-tap"
            description = "A homebrew tap for my own formulas and casks."
        }
        menubar-scripts = {
            name = "menubar-scripts"
            description = ""
        }
        mvnx = {
            name = "mvnx"
            description = "An experiment with Maven dependencies and dynamic classloading"
        }
        myprojects-cleaner = {
            name = "myprojects-cleaner"
            description = ""
        }
        newlinechecker = {
            name = "newlinechecker"
            description = "A sample project to play with GraalVM builds on GitHub Actions"
        }
        nope-amine = {
            name = "nope-amine"
            description = ""
        }
        postgres-sync-demo = {
            name = "postgres-sync-demo"
            description = "A demo on how to use triggers, queues, etc. to sync the app's data somewhere else."
        }
        python-nc = {
            name = "python-nc"
            description = ""
        }
        rss-to-mail = {
            name = "rss-to-mail"
            description = "Read from RSS feeds and send an email for every new item."
        }
        sci-fi-movies = {
            name = "sci-fi-movies"
            description = ""
        }
        testcontainers-colima-github-actions = {
            name = "testcontainers-colima-github-actions"
            description = ""
        }
        vagrant-ssh-config = {
            name = "vagrant-ssh-config"
            description = ""
        }
        wait-for-ports = {
            name = "wait-for-ports"
            description = "A command-line utility that waits until a port is open."
        }
        webapp-classloader-test = {
            name = "webapp-classloader-test"
            description = "This is a test that can be used during integration testing to check for classloader leaks."
        }
        website-janitor = {
            name = "website-janitor"
            description = "A set of tools that check websites for common misconfigurations or downtime."
        }
  }
}
