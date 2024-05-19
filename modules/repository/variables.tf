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

variable "pages_build_type" {
    description = "GitHub pages build type"
    type = string
}

variable "archived" {
    description = "(Optional) Specifies if the repository should be archived."
    default = false
}
