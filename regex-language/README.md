This subproject contains the Langium language of our `.regex` files. You need to edit the grammar in [`src/regex.langium`](src/regex.langium), and write test regexes into the `test/valid` and `test/invalid` folders. Everything else is infrastructure that we have already set up for you.

## Editing the Grammar

To edit the grammar, install the Langium extension (`langium.langium-vscode`) in VSCode. VSCode should recommend it when you open the repository.

## Trying Out the Language

To try out your language, open the `Run and Debug` view (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>D</kbd>), and choose the `Regex Editor` launch configuration (or just press <kbd>F5</kbd>).

This launch configuration regenerates the parser, bundles the extension, and starts a new VSCode instance called the **extension host** with the regex extension installed. The extension host opens the `regex-language/test` folder. Open any `.regex` file there, and you should get syntax highlighting, error markers for syntax errors and unknown names, content assist, and go to definition.

Whenever you change the grammar, close the extension host and start `Regex Editor` again.

## Testing the Grammar

Every `.regex` file in `test/valid` must be accepted by your grammar, and every `.regex` file in `test/invalid` must be rejected. Write these files based on the specification, even before your grammar can handle them. 

To add a test, put a new `.regex` file into the folder of the outcome you expect. There is nothing to register anywhere: the file is the test, and its name is the name of the test. Keep one thing per file and name it after what it checks, so that a failing build already tells you what broke.

The tests run as part of the build, or on their own with `./gradlew :regex-language:test`.
