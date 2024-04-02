# Features

- Authorization in your telegram account
- Fetching info about your telegram profile through tdlib library
- Searching channels
- Searching users
- Tracking user activity while cross promotion is ongoing
- Creating simple report with joined users in it

# Getting started

- Make sure that postgres config in application.properties is correct
- Make sure that you use correct native library of tdlight. Other native libraries can be
  found [here](https://github.com/tdlight-team/tdlight-java#natives-inclusion)
- After application starts, you can find swagger-ui [here](http://localhost:8080/swagger/)

# Other

If you want change apiId and apiHash, go [here](https://my.telegram.org/). Then
change it in application.properties.