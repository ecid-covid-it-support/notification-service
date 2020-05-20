#!/usr/bin/env sh

# Export private key and SSL certificate to P12 keystore
echo "Generating keystore"
openssl pkcs12 -export -in ${SSL_CERT_PATH} -inkey ${SSL_KEY_PATH} -out /etc/.certs/tmp_keystore.p12 -passout pass:${KEYSTORE_PASS} -name notification
echo "Remove old certificate from truststore"
keytool -delete -alias notification -keystore $JAVA_HOME/lib/security/cacerts -noprompt -storepass changeit
keytool -delete -alias mongo -keystore $JAVA_HOME/lib/security/cacerts -noprompt -storepass changeit
echo "Importing to truststore"
keytool -import -file ${SSL_CERT_PATH} -alias notification -keystore $JAVA_HOME/lib/security/cacerts -noprompt -storepass changeit
keytool -import -file ${MONGO_PEM_KEY_PATH} -alias mongo -keystore $JAVA_HOME/lib/security/cacerts -noprompt -storepass changeit


echo "Starting notification APP"
java -jar OCARIoT-1.0-SNAPSHOT.jar
