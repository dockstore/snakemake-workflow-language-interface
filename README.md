# icgc-storage-client-plugin
Dockstore icgc file provisioning plugin

## Usage

The s3 plugin is capable of downloading files by calling out to an installed copy of the [icgc-storage-client](http://docs.icgc.org/cloud/guide/#storage-client-usage). 

```
$ cat test.icgc.json 
{
    "input_file": {
        "class": "File",
        "path": "icgc://097ddb14-9be7-5147-9d6e-7d350e7b203e"
    },
    "output_file": {
        "class": "File",
        "path": "s3://oicr.temp/bamstats_report.zip"
    }
}

$ dockstore tool launch --entry  quay.io/briandoconnor/dockstore-tool-md5sum  --json test.icgc.json 
Creating directories for run of Dockstore launcher at: ./datastore//launcher-2ebce330-2a44-4a3a-9d6d-55c152a5c38e
Provisioning your input files to your local machine
Downloading: #input_file from icgc://097ddb14-9be7-5147-9d6e-7d350e7b203e into directory: /media/large_volume/dockstore_tools/dockstore-tool-md5sum/./datastore/launcher-2ebce330-2a44-4a3a-9d6d-55c152a5c38e/inputs
/a91c615b-a8ec-452f-afef-e6da6032194d
Calling on plugin io.dockstore.provision.ICGCStorageClientPlugin$ICGCStorageClientProvision to provision icgc://097ddb14-9be7-5147-9d6e-7d350e7b203e
...
```


## Configuration 

This plugin gets configuration information from the following structure in ~/.dockstore/config

```
[dcc_storage]
client = /media/large_volume/icgc-storage-client-1.0.23/bin/icgc-storage-client
```

Set the client location to your own and also make sure that the configuration file is populated with an access token (ex: /media/large_volume/icgc-storage-client-1.0.23/conf/application.properties ) and that the `STORAGE_PROFILE=collab` variable is set in your environment if applicable. 


