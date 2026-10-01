This subproject contains the Refinery metamodel of our regexes. You need to edit the metamodel in [`src/main/refinery/regex.refinery`](src/main/refinery/regex.refinery), and write example models into `src/test/refinery`.

## Editing the Metamodel

We use the [Refinery web editor](https://refinery.services/) to edit the metamodel.

The web editor does not save anything into the repository, so don't forget to copy your changes back into `regex.refinery`.

## Example Models

The example models are instance models of your metamodel, each in its own `.problem` file starting with `import regex.`. Every model in `src/test/refinery/consistent` must be consistent with your metamodel, and every model in `src/test/refinery/inconsistent/<name>` must violate the error predicate called `<name>`. The tests also check that your metamodel itself has no errors, is consistent, and Refinery can generate models from it.

To add a consistent example, put a new `.problem` file into `src/test/refinery/consistent`. To add a violating one, put it into `src/test/refinery/inconsistent/<name>`, and create that folder if the error predicate does not have one yet. The folder name is how the test knows which predicate the model has to violate, so every error predicate you write needs its own folder.

The tests run as part of the build, or on their own with `./gradlew :regex-metamodel:test`.
