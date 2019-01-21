# icgc-storage-client-plugin
Dockstore icgc score client file provisioning plugin

## Usage

The icgc score client plugin is capable of downloading files by calling out to a Docker image of the [icgc-score-client](https://docs.icgc.org/download/guide/#score-client-usage).  Currently it has only been tested with downloading a directory of files from icgc.

```
$ cat dockstore.wdl 
task stat {
  File dir
  command {
    stat ${dir}
  }
  output {
    String outf = read_string(stdout())
  }
}
workflow dir_check {
  call stat
}

$ cat dockstore.json
{
  "dir_check.stat.dir": "icgc://eeca3ccd-fa4e-57bf-9fde-c9d0ddf69935"
}

$ dockstore workflow launch --local-entry dockstore.wdl --json dockstore.json
Creating directories for run of Dockstore launcher in current working directory: /home/user/testCLI
Provisioning your input files to your local machine
Downloading: dir_check.stat.dir from icgc://eeca3ccd-fa4e-57bf-9fde-c9d0ddf69935 to: /home/user/testCLI/cromwell-input/1ad7fb3d-62bc-4e1a-8d5d-cc173c0d4e82/icgc:/eeca3ccd-fa4e-57bf-9fde-c9d0ddf69935
Calling on plugin io.dockstore.provision.ICGCStorageClientPlugin$ICGCStorageClientProvision to provision icgc://eeca3ccd-fa4e-57bf-9fde-c9d0ddf69935
...
```


## Configuration 

This plugin gets configuration information from the following structure in ~/.dockstore/config

```
[dockstore-file-icgc-storage-client-plugin]
client-key = ########-####-####-####-############
```

[Docker](https://www.docker.com/) will need to be installed 


