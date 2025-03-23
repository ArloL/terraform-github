variable "name" {
    description = "Name of the repository"
    type        = string
}

variable "description" {
    description = "Description of the repository"
    type        = string
}

variable "homepage" {
    description = "Homepage of the repository"
    type        = string
}

variable "main_branch" {
    description = "The main branch of the repository"
    type = string
}

variable "github_pages" {
    description = "Enable GitHub Pages"
    type = bool
    default = false
}

variable "archived" {
    description = "(Optional) Specifies if the repository should be archived."
    type = bool
    default = false
}

variable "required_status_checks" {
    description = "(Optional) The list of status checks to require in order to merge into this branch. No status checks are required by default."
    type = list(string)
    default = []
}

variable "actions_secrets" {
    description = "(Optional) a map of secrets added to the repository in the actions scope"
    type = map(string)
    default = {}
}

variable "visibility" {
    description = "(Optional) Can be public or private. If your organization is associated with an enterprise account using GitHub Enterprise Cloud or GitHub Enterprise Server 2.20+, visibility can also be internal. The visibility parameter overrides the private parameter."
    type = string
    default = "public"
}
