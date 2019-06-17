RestGun
=========================

[![Gitter](https://badges.gitter.im/restgun/community.svg)](https://gitter.im/restgun/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

RestGun is a platform for automating the REST API TESTING by writing flexible javascript based assertions.

---
## Using RestGun

To get started using RestGun, you just need a very basic understanding of java,javascript

## Main Features
- Create Rest Test cases on the fly
- Rest Rules(Assertions) through Java Script
- Tagging and Grouping of test cases
- Generate Report and Share with anybody

---
## Modules
- www = UI HTML and Javascript files
- src = source java files

## Installation
RestGun uses ElasticSearch as the backend datastorage mechanism. By default the configuration in application.properties under src/resources folder is : es.url=http://localhost:9200/
Make sure ElasticSearch with the provided configuration is Up and Running before moving to RestGun.

    git clone https://github.com/dhaneeshtnair/restgun.git
    mvn clean install
    java -jar target/restgun-1.0.jar
    Server will start on htttp://localhost:44444
Start creating assertions by clicking on UI
#### Use Maven Central Repository
Use maven central repository to download dependant Jars.
## Contribute
1. Check for open issues or open a fresh one to start a discussion around a feature idea or a bug.
2. If you feel uncomfortable or uncertain about an issue or your changes, don't hesitate to contact us on Gitter using the link above.
3. Fork [the repository](https://github.com/deeplearning4j/deeplearning4j.git) on GitHub to start making your changes to the **master** branch (or branch off of it).
4. Write a test which shows that the bug was fixed or that the feature works as expected.
5. Send a pull request and bug us on Gitter until it gets merged and published. :)

## Licence
MIT
Change the world the way you wish.