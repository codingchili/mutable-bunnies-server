# Security

Creates a keypair with a subject alt name for localhost. Import this into trusted
CA's to run https over localhost in chrome.

1. Generate keypair

```console
openssl req -x509 -nodes -days 730 -newkey rsa:3072 -keyout cert.key -out cert.pem -config req.cnf -sha256
 ```
 
 2. Import keypair into jks

```console
$ keytool -keystore keystore.jks -importcert -trustcacerts -file cert.pem
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