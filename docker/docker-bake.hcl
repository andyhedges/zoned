group "default" {
  targets = ["zoned"]
}

group "github" {
  targets = ["zoned-gha"]
}

variable "VERSION" {
  default = "dev"
}

target "zoned" {
  context    = "."
  dockerfile = "docker/Dockerfile"
  platforms  = ["linux/amd64", "linux/arm64"]
  tags       = ["ghcr.io/andyhedges/zoned:${VERSION}", "ghcr.io/andyhedges/zoned:latest"]
}

target "zoned-gha" {
  inherits   = ["zoned"]
  cache-from = ["type=gha"]
  cache-to   = ["type=gha,mode=max"]
  output     = ["type=registry"]
}
