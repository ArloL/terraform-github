terraform {
    required_providers {
        integrations-github = {
            source  = "integrations/github"
            version = "~> 6.0"
        }
        github = {
            source  = "hashicorp/github"
            version = "~> 6.0"
        }
    }
}
