variable "repository" {
    description = "Map of repository names"
    type = list(object({
        name = string
        description = string
        archived = optional(bool)
        homepage = optional(string)
        pages_build_type = optional(string)
        required_status_checks = optional(list(string))
    }))
    default = [
        {
            name = "abenteuer-irland"
            description = "Mum's website for Abenteuer Irland"
            homepage = "https://arlol.github.io/abenteuer-irland/"
            pages_build_type = "workflow"
        },
        {
            name = "actions"
            description = "A demonstration of an automated release workflow for shared workflows and actions"
        },
        {
            name = "angular-playground"
            description = "A playground for the Angular framework"
            homepage = "https://arlol.github.io/angular-playground/"
            pages_build_type = "workflow"
        },
        {
            name = "arlol.github.io"
            description = "This is the source of my GitHub page"
            homepage = "https://arlol.github.io/"
            pages_build_type = "workflow"
        },
        {
            name = "bulma-playground"
            description = "A playground for the Bulma CSS framework"
            homepage = "https://arlol.github.io/bulma-playground/"
            pages_build_type = "workflow"
        },
        {
            name = "business-english"
            description = "Mum's website for Business English"
            homepage = "https://arlol.github.io/business-english/"
            pages_build_type = "workflow"
        },
        {
            name = "calver-tag-action"
            description = "A GitHub Actions action that creates a new version using a CalVer-style derivative and pushes it"
        },
        {
            name = "chorito"
            description = "A tool that does some chores in your source code"
            required_status_checks = [ "deploy", "linux", "macos", "windows" ]
        },
        {
            name = "corporate-python"
            description = "A container for executing python in corporate environments"
        },
        {
            name = "dotfiles"
            description = "My collection of dotfiles used to configure my command line environments"
        },
        {
            name = "eclipse-projects"
            description = "Arlo's project catalog for the Eclipse Installer"
            homepage = "https://arlol.github.io/eclipse-projects/"
            pages_build_type = "workflow"
        },
        {
            name = "effortful-retrieval-questions"
            description = "A collection of effortful retrieval questions of a number of articles I've read"
        },
        {
            name = "git-dora-lead-time-calculator"
            description = "A project to calculate the DORA metric lead time with the info from a git repo"
        },
        {
            name = "git-presentation-2018-10"
            description = "Git Präsentation für Vorlesung Industrielle Softwareentwicklung"
        },
        {
            name = "gitfx"
            description = "A simple git client with Java 11+, JavaFX 15+ and GraalVM"
            archived = true
        },
        {
            name = "graalfx"
            description = "A simple desktop app with JavaFX and GraalVM"
            archived = true
        },
        {
            name = "kafka-debugger"
            description = "A small jar utility to test kafka connections"
        },
        {
            name = "homebrew-tap"
            description = "A homebrew tap for my own formulas and casks"
        },
        {
            name = "menubar-scripts"
            description = "A collection of scripts that can run in e.g. xbar, swiftbar, etc."
        },
        {
            name = "mvnx"
            description = "An experiment with Maven dependencies and dynamic classloading"
        },
        {
            name = "myprojects-cleaner"
            description = "A java application that runs git clean in a bunch of directories"
        },
        {
            name = "newlinechecker"
            description = "A sample project to play with GraalVM builds on GitHub Actions"
        },
        {
            name = "nope-amine"
            description = "A firefox extension that slowly increases the time for things to load on reddit.com"
        },
        {
            name = "pico-playground"
            description = "A playground for the Pico CSS framework"
            homepage = "https://arlol.github.io/pico-playground/"
            pages_build_type = "workflow"
            archived = true
        },
        {
            name = "postgres-sync-demo"
            description = "A demo on how to use triggers, queues, etc. to sync the app's data somewhere else"
        },
        {
            name = "python-nc"
            description = "A test to see if I can implement nc's proxy functionality with python"
        },
        {
            name = "rss-to-mail"
            description = "Read from RSS feeds and send an email for every new item"
        },
        {
            name = "sci-fi-movies"
            description = "an app to import sci fi movies from rotten tomatoes into a database in order to run queries on them"
        },
        {
            name = "self-hosted-gh-actions-runner"
            description = "An experiment to test docker-based builds inside a GitHub Actions Runner that is a running container itself"
            archived = true
        },
        {
            name = "terraform-github"
            description = "A project to manage github settings with terraform"
        },
        {
            name = "testcontainers-colima-github-actions"
            description = "A demo project of running testcontainers with colima on GitHub Actions"
            archived = true
        },
        {
            name = "vagrant-ssh-config"
            description = "A vagrant plugin that automatically creates ssh configs for vms"
        },
        {
            name = "vitest-link-reproduction"
            description = "This project demonstrates an issue where vitest runs fail when a dependency is included via a link"
        },
        {
            name = "wait-for-ports"
            description = "A command-line utility that waits until a port is open"
        },
        {
            name = "webapp-classloader-test"
            description = "This is a test that can be used during integration testing to check for classloader leaks"
        },
        {
            name = "website-janitor"
            description = "A set of tools that check websites for common misconfigurations or downtime"
        }
    ]
}
