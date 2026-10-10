# Lemonade Stand (Java)

You can find the instructions for this portfolio [here](https://morethanequations.com/Computer-Science/Portfolio-Projects/Lemonade-Stand/Core-Game-Loop). Create a new repository on GitHub to house your code. Be sure to make the repository public so that I can view and grade it.

We will use [Gradle](https://gradle.org/) to automate common development tasks.

## Building the App

You can build the app using:

```bash
./gradlew build
```

## Testing the App

You can run the automated suite of tests using:

```bash
./gradlew test
```

## Running the App

You can run the app using:

```bash
./gradlew run --quiet --console=plain
```

The two flags passed to the `run` command hide the noisy output from Gradle. You can see the details from Gradle by omitting those flags.
