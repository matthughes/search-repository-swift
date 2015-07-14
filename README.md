Swift repository plugin for Elasticsearch
=========================================

In order to install the plugin, simply run: `bin/plugin -install org.wikimedia.elasticsearch.swift/swift-repository-plugin/<version>`.

|      Swift Plugin           | elasticsearch         | Release date |
|-----------------------------|-----------------------|:------------:|
| 0.4                         | 1.1.0                 | 2014-05-28   |
| 0.6                         | 1.3.2                 | 2014-08-20   |
| 0.7                         | 1.4.0                 | 2014-11-07   |
| 0.8-SNAPSHOT                | master                |              |

Versions 0.4, 0.6 and 0.7 should be used. The in-between releases were
buggy and are not recommended.

## Create Repository
```
    $ curl -XPUT 'http://localhost:9200/_snapshot/my_backup' -d '{
        "type": "swift",
        "settings": {
            "swift_url": "http://localhost:8080/auth/v1.0/",
            "swift_container": "my-container",
            "swift_username": "myuser",
            "swift_password": "mypass!"
        }
    }'
```

## Create Repository using Keystone
```
    $ curl -XPUT 'http://localhost:9200/_snapshot/my_backup' -d '{
        "type": "swift",
        "settings": {
            "swift_url": "http://localhost:5000/v2.0/tokens",
  	    "swift_authmethod": "KEYSTONE",
            "swift_container": "my-container",
            "swift_username": "myuser",
            "swift_password": "mypass!",
            "swift_region": "us_eastcoast"
        }
    }'
```

See [Snapshot And Restore](http://www.elasticsearch.org/guide/en/elasticsearch/reference/1.x/modules-snapshots.html) for more information


## Settings
|  Setting                            |   Description
|-------------------------------------|------------------------------------------------------------
| swift_container                     | Swift container name. **Mandatory**
| swift_url                           | Swift auth url. **Mandatory**
| swift_authmethod                    | Swift auth method, one of "KEYSTONE" "TEMPAUTH" or "" for basic auth
| swift_password                      | Swift password
| swift_tenant                        | Swift tenant name, only used with keystone auth
| swift_username                      | Swift username
| swift_region                        | Swift preffered region
| chunk_size                          | Maximum size for individual objects in the snapshot. Defaults to `5gb` as that's the Swift default
| compress                            | Turns on compression of the snapshot files. Defaults to `false` as it tends to break with Swift
| max_restore_bytes_per_sec           | Throttles per node restore rate. Defaults to `20mb` per second.
| max_snapshot_bytes_per_sec          | Throttles per node snapshot rate. Defaults to `20mb` per second.


## To debug in Eclipse
Since Swift has logging dependencies you have to be careful about debugging in Eclipse.

1.  Import this project into Eclipse using the maven connector.  Do no import the main Elasticsearch code.
2.  Create a new java application debug configuration and set it to run ElasticsearchF.
3.  Go to the Classpath tab
4.  Click on Maven Dependiences
5.  Click on Advanced
6.  Click Add Folder
7.  Click ok
8.  Expand the tree to find <project-name>/src/test/resources
9.  Click ok
10. Click debug
