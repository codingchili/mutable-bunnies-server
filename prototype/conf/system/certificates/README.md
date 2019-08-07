# Security

Creates a keypair with a subject alt name for localhost. Import this into trusted
CA's to run https over localhost in chrome.

1. Generate keypair

```console
$ openssl req -x509 -nodes -days 730 -newkey rsa:3072 -keyout cert.key -out cert.pem -config req.cnf -sha256
$ openssl pkcs12 -export -in cert.pem -inkey cert.key -out keystore.pkcs12 -name tlskey
 ```
 
 2. Import keypair into jks

```console
$ keytool -importkeystore -deststorepass secret -destkeypass secret -destkeystore keystore.jks -srckeystore keystore.pkcs12 -srcstoretype PKCS12 -srcstorepass secret -alias tlskey
```



3. Configure keystore in config/security.yaml

```yaml
keystores:
  - path: 'keystore.jks'
    password: 'secret'
```

Enable security for

- routingserver.yaml
- webserver.yaml
- realms.

```yaml
secure: true
keystore: default
```

Finally, browse the website with chrome and export the untrusted certificate, then import it as a trusted ca cert.