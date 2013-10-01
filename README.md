s3up
====

Simple S3 upload tool

Download:
* https://s3.amazonaws.com/vgv-public/s3sync/s3up.jar
* https://s3.amazonaws.com/vgv-public/s3sync/s3down.jar


Examples:
```
java -jar s3up.jar -help
```

```
java -jar s3up.jar -accessKey XXX -secretKey XXX -bucket test_bucket -key /some_folder -local /local_file_or_folder
```

```
java -jar s3up.jar -accessKey XXX -secretKey XXX -bucket test_bucket -key /some_folder -local /local_file_or_folder -threads 5 -rrs
```

