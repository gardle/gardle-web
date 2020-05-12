# Gardle

### Notes for application configuration

In order to use this application, please make sure you copy the `application.properties.example` to either `application.properties` or `application-{profile}.properties` (to overwrite previous values).
If you desire to use reverse/forward geocoding, please see the files `config/(dev|prod).env.js`, the implemented geocoding uses _LocationIQ_, which provide a free tier, just paste your API_KEYS in there.
For production, a docker stack will be deployed. There are some DB user/passwords which need to be configured in `gardle-stack.yml` in order to be spawnable.
Application analytics are implemented using Matomo, thus in the `matomo.yml` there is need for configuration of the database user for Matomo.
If you were to translate the application provided texts (labels, etc) you can get a Google Translate API key and run the bash script located in `src/main/webapp/i18n/translate-all.sh`.

## Development

Before you can build this project, you must install and configure the following dependencies on your machine:

1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in [package.json](package.json).

    npm install

We use npm scripts and [Webpack][] as our build system.

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

    ./mvnw
    npm start

Npm is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `npm update` and `npm install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `npm help update`.

The `npm run` command will list all of the scripts available to run for this project.

## Building for production

To optimize the Gardle application for production, run:

    ./mvnw -Pprod clean package

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar target/*.war

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.

## Testing

To launch your application's tests, run:

    ./mvnw clean test

### Client tests

Unit tests are run by [Jest][] and written with [Jasmine][]. They're located in [src/test/javascript/](src/test/javascript/) and can be run with:

    npm test

For more information, refer to the [Running tests page][].
