variable "repository" {
    description = "Map of repository names"
    type = map(any)
    default = {
        abenteuer-irland = {
            name = "abenteuer-irland"
            description = "Mum's website for Abenteuer Irland"
            homepage = "https://arlol.github.io/abenteuer-irland/"
            pages_build_type = "workflow"
            pages_source_branch = "main"
        }
        "arlol.github.io" = {
            name = "arlol.github.io"
            description = "This is the source of my GitHub page"
            pages_build_type = "workflow"
            pages_source_branch = "main"
        }
        business-english = {
            name = "business-english"
            description = "Mum's website for Business English"
            pages_build_type = "workflow"
            pages_source_branch = "main"
        }
        calver-tag-action = {
            name = "calver-tag-action"
            description = "A GitHub Actions action that creates a new version using a CalVer-style derivative and pushes it"
        }
        chorito = {
            name = "chorito"
            description = "A tool that does some chores in your source code"
        }
        corporate-python = {
            name = "corporate-python"
            description = "A container for executing python in corporate environments"
        }
        dotfiles = {
            name = "dotfiles"
            description = "My collection of dotfiles used to configure my command line environments"
        }
        eclipse-projects = {
            name = "eclipse-projects"
            description = "Arlo's project catalog for the Eclipse Installer"
            homepage = "https://arlol.github.io/eclipse-projects/"
            pages_build_type = "workflow"
            pages_source_branch = "main"
        }
        effortful-retrieval-questions = {
            name = "effortful-retrieval-questions"
            description = "A collection of effortful retrieval questions of a number of articles I've read"
        }
        git-dora-lead-time-calculator = {
            name = "git-dora-lead-time-calculator"
            description = "A project to calculate the DORA metric lead time with the info from a git repo"
        }
        git-presentation-2018-10 = {
            name = "git-presentation-2018-10"
            description = "Git Präsentation für Vorlesung Industrielle Softwareentwicklung"
        }
        gitfx = {
            name = "gitfx"
            description = "A simple git client with Java 11+, JavaFX 15+ and GraalVM"
        }
        graalfx = {
            name = "graalfx"
            description = "A simple desktop app with JavaFX and GraalVM"
        }
        homebrew-tap = {
            name = "homebrew-tap"
            description = "A homebrew tap for my own formulas and casks"
        }
        menubar-scripts = {
            name = "menubar-scripts"
            description = "A collection of scripts that can run in e.g. xbar, swiftbar, etc."
        }
        mvnx = {
            name = "mvnx"
            description = "An experiment with Maven dependencies and dynamic classloading"
        }
        myprojects-cleaner = {
            name = "myprojects-cleaner"
            description = "A java application that runs git clean in a bunch of directories"
        }
        newlinechecker = {
            name = "newlinechecker"
            description = "A sample project to play with GraalVM builds on GitHub Actions"
        }
        nope-amine = {
            name = "nope-amine"
            description = "A firefox extension that slowly increases the time for things to load on reddit.com"
        }
        postgres-sync-demo = {
            name = "postgres-sync-demo"
            description = "A demo on how to use triggers, queues, etc. to sync the app's data somewhere else"
        }
        python-nc = {
            name = "python-nc"
            description = "A test to see if I can implement nc's proxy functionality with python"
        }
        rss-to-mail = {
            name = "rss-to-mail"
            description = "Read from RSS feeds and send an email for every new item"
        }
        sci-fi-movies = {
            name = "sci-fi-movies"
            description = "an app to import sci fi movies from rotten tomatoes into a database in order to run queries on them"
        }
        self-hosted-gh-actions-runner = {
            name = "self-hosted-gh-actions-runner"
            description = "An experiment to test docker-based builds inside a GitHub Actions Runner that is a running container itself"
        }
        terraform-github = {
            name = "terraform-github"
            description = "A project to manage github settings with terraform"
        }
        testcontainers-colima-github-actions = {
            name = "testcontainers-colima-github-actions"
            description = "A demo project of running testcontainers with colima on GitHub Actions"
        }
        vagrant-ssh-config = {
            name = "vagrant-ssh-config"
            description = "A vagrant plugin that automatically creates ssh configs for vms"
        }
        vitest-link-reproduction = {
            name = "vitest-link-reproduction"
            description = "This project demonstrates an issue where vitest runs fail when a dependency is included via a link"
        }
        wait-for-ports = {
            name = "wait-for-ports"
            description = "A command-line utility that waits until a port is open"
        }
        webapp-classloader-test = {
            name = "webapp-classloader-test"
            description = "This is a test that can be used during integration testing to check for classloader leaks"
        }
        website-janitor = {
            name = "website-janitor"
            description = "A set of tools that check websites for common misconfigurations or downtime"
        }
  }
}
