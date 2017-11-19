# Captain
Service Discovery
 
## Getting started

....
 
## Config

##### Environment variables
In order to start this service, one must provide the following environment variables:
> Note the formatting for each var
* **ZOOKEEPER_ADDRESS** - `<host>:<port>,<host>:<port>...` - A list (separated by a comma) of zookeeper nodes
* **ZOOKEEPER_CONNECTION_RETRY_MS** - `milliseconds` - An integer representing the milliseconds between each connection retry
* **DISCOVERY_SERVICE_NAME** - `string` - A string representing the name of this service (The ServiceDiscovery service)
* **HEARTBEAT_THRESHOLD** - `milliseconds` - An integer representing the amount of milliseconds that a service is considered alive
