pipe
====

Named pipes

```

import module namespace pipe = "http://exist-db.org/pipe";

let $pipeID := pipe:make()

let $tmp := pipe:write($pipeID, "test message")

let $msg := pipe:read($pipeID)

let $tmp := pipe:close($pipeID)

return $msg

```
