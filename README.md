# OCARIoT Notification Microservice

Microservice responsible for sending notifications to user's client apps of OCARIoT platform.

**Main features:**

-   Send cloud notification using Firebase;
-   Save user's Firebase tokens;
-   Save and return pending notifications;

See the  [documentation](https://github.com/ocariot/notification-service/wiki) for more information.

## Prerequisites

-   [Java 8+](https://www.oracle.com/java/technologies/javase-downloads.html)
-   [MongoDB Server 3.0.0+](https://www.mongodb.com/download-center/community)
-   [RabbitMQ 3.7.0+](https://www.rabbitmq.com/download.html)

----------
## Generate Certificates

For development and testing environments the easiest and fastest way is to generate your own self-signed certificates. These certificates can be used to encrypt data as well as certificates signed by a CA, but users will receive a warning that the certificate is not trusted for their computer or browser. Therefore, self-signed certificates should only be used in non-production environments, that is, development and testing environments. To do this, run the  `create-self-signed-certs.sh`script in the root of the repository.

./create-self-signed-certs.sh

The following files will be created:  `ca.crt`,  `server.crt`  and  `server.key`.

In production environments its highly recommended to always use valid certificates and provided by a certificate authority (CA). A good option is  [Let's Encrypt](https://letsencrypt.org/)  which is a CA that provides free certificates. The service is provided by the Internet Security Research Group (ISRG). The process to obtain the certificate is extremely simple, as it is only required to provide a valid domain and prove control over it. With Let's Encrypt, you do this by using  [software](https://certbot.eff.org/)  that uses the ACME protocol, which typically runs on your host. If you prefer, you can use the service provided by the  [SSL For Free](https://www.sslforfree.com/)  website and follow the walkthrough. The service is free because the certificates are provided by Let's Encrypt, and it makes the process of obtaining the certificates less painful.

If you are not running the latest docker image available of this microservice, but instead you choose to running alone you will also need to import the certificate into the Java Keystore and Truststore  using the following commands:

`openssl pkcs12 -export -in server.crt -inkey server.key -out tmp_keystore.p12 -passout pass:s3cr3tp4ss -name notification`

`keytool -import -file server.crt -alias notification -keystore path/to/truststore/cacerts -noprompt -storepass changeit`


## Using Docker

In the Docker Hub, you can find the image of the most recent version of this repository. With this image it is easy to create your own containers.

`docker run ocariot/notification`

This command will download the latest image and create a container with the default settings.

The Notification Microservice depends of the Account service to function properly, so the best way is to use the available [docker-compose](https://github.com/ocariot/docker-compose) and run it either using the notification image or as a detached program.

If you are running this repository as a detached program you need to set the enviromental variables and expose the port for mongodb.

## Set the environment variable

Application settings are defined by environment variable.. The enviroment variables necessary by the notification microservice are:

|VARIABLE  | DESCRIPTION |DEFAULT|
|--|--|--|
| `PORT_HTTPS`| Port used to listen for HTTPS requests. Do not forget to provide the private key and the SSL/TLS certificate. See the topic generate certificates.| `10001` |
| `MONGODB_URI` |Database connection URI used if the application is running in development or production environment. The [URI specifications](https://docs.mongodb.com/manual/reference/connection-string) defined by MongoDB are accepted. For example: `mongodb://user:pass@host:port/database?options` | `mongodb://${NOTIFICATION_DB_USER}:${NOTIFICATION_DB_PASS}@mongo-notification:27017/notification?ssl=true`|
| `MONGO_NOTIFICATION_DATABASE`| Name of the database created for the microservice| `notification`|
| `RABBITMQ_HOST`| Host for the rabbitmq connecton  |`rabbitmq` |
| `RABBITMQ_PORT`| Port for the rabbitmq connection|`${RABBITMQ_PORT:-5672}`|
| `RABBITMQ_USERNAME`| Username for Notification Service to connect RabbitMQ.| `${NOTIFICATION_RABBITMQ_USER}`|
| `RABBITMQ_PASSWORD`| Password for Notification Service to connect to the RabbitMQ. | `${NOTIFICATION_RABBITMQ_PASS}`|
| `RABBITMQ_VHOST` |RabbitMQ Virtual Host where Queues, Exchanges and Bindings are present. | `ocariot`|
| `RABBITMQ_SSL`| Flag that enables/disables ssl connection to RabbitMQ.|`false` |
| `KEYSTORE_PASS` | PKCS12 file pass required in the Notification service. | `${KEYSTORE_PASS}`|
| `GOOGLE_APPLICATION_CREDENTIALS`|Path to the Firebase authentication file. |`/etc/keys/firebase_keyfile.json` |
| `MESSAGES_PATH`| File path to the pre-set notification messages file.| `/etc/keys/messages.json`|


Some of the environment variables are passed by another environment variables that are set in the file `.env.example`.
Copy and paste with the file `.env.example` and name it `.env` to make the Docker Compose use the environment variables defined in this file:

|VARIABLE  | DESCRIPTION |DEFAULT|
|--|--|--|
| `SSL_KEY_PATH` |Private key for SSL certificate. | `./certs/server.key` |
| `SSL_CERT_PATH`| Certificate SSL. | `./certs/server.crt`|
|`KEYSTORE_PASS` | PKCS12 file pass required in the Notification service. | `s3cr3tp4ss`|
|`FIREBASE_KEYFILE_PATH` |File that must be obtained in the [Firebase project settings](https://firebase.google.com/docs/admin/setup?gclid=CjwKCAjw-YT1BRAFEiwAd2WRtkXLw8yNy11nuoWcGTH1mvwYSucmcuzJ9SXDSvBO-jDqX-8kA3efjxoCxdUQAvD_BwE#java). |`./firebase_keyfile.json` |
| `MESSAGES_FILE_PATH`|File that contains pre-set notification type messages. | `./config/notification/messages.json`|
| `NOTIFICATION_RABBITMQ_USER`| Username for Notification Service to connect RabbitMQ.|`notification.app` |
| `NOTIFICATION_RABBITMQ_PASS`| Password for Notification Service to connect to the RabbitMQ.|`RABBITMQ_PASSWORD` |

 If you are not running this repository using the docker image make sure you set the enviromental variables of the first table to the desired ones.
