Squawk
======

CLI tools for API development

Squawk provides a simple CLI tool for running predefined API requests in
your application and seeing the output directly in the command line.

Getting Started
---------------

Download the [latest][1] `squawk` binary and place it in your
project root.

Define your API requests in an `api.squawk` file:

```kotlin
endpoint {
  url = "https://example.com"
}
```

Run your endpoint with:

    ./squawk

[1]: https://github.com/InkApplications/Squawk/releases/latest

Configure Endpoints
-------------------

Name multiple endpoints:

```kotlin
endpoint {
    name = "get-widgets"
    description = "List all widgets"
    url = "https://example.com/widgets"
}
endpoint {
    name = "create-widget"
    description = "Create a new widget"
    url = "https://example.com/widgets"
    method = POST
    body = """
      {
        "name": "Example"
      }
    """
}
```

Run a specific endpoint:

    ./squawk get-widgets

List all endpoints:

    ./squawk --list

Includes
--------

Include additional configurations:

```kotlin
include("samples/example.squawk")
```

Namespace your files:

```kotlin
namespace = "samples"

endpoint {
    name = "get"
    url = "https://example.com"
}
```

Run with:

    ./squawk samples:get

