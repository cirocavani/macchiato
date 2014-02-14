Macchiato Finagle Sample
------------------------

http://twitter.github.io/finagle/

*Results*

    (first, start Redis and set 'foo'='bar')
    
    sbt/sbt run

...

    curl -D - http://127.0.0.1:8080/foo
    
    HTTP/1.1 200 OK
    Content-Type: text/plain
    Content-Length: 3
    
    bar
...

    wrk -t 10 -c 200 -d 30s --latency http://127.0.0.1:8080/foo
    
    Running 30s test @ http://127.0.0.1:8080/foo
      10 threads and 200 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     6.28ms    6.61ms 240.09ms   96.53%
        Req/Sec     3.41k   688.51    12.33k    75.54%
      Latency Distribution
         50%    5.34ms
         75%    7.29ms
         90%    9.74ms
         99%   17.41ms
      964449 requests in 29.99s, 61.62MB read
    Requests/sec:  32154.28
    Transfer/sec:      2.05MB

...
