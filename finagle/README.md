Macchiato Finagle Sample
------------------------

http://twitter.github.io/finagle/

*Http Server Simple*

    curl -v http://127.0.0.1:8080
    
    * Rebuilt URL to: http://127.0.0.1:8080/
    * About to connect() to 127.0.0.1 port 8080 (#0)
    *   Trying 127.0.0.1...
    * Adding handle: conn: 0x25a38f0
    * Adding handle: send: 0
    * Adding handle: recv: 0
    * Curl_addHandleToPipeline: length: 1
    * - Conn 0 (0x25a38f0) send_pipe: 1, recv_pipe: 0
    * Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
    > GET / HTTP/1.1
    > User-Agent: curl/7.32.0
    > Host: 127.0.0.1:8080
    > Accept: */*
    > 
    < HTTP/1.1 200 OK
    < Content-Type: text/plain; charset=utf-8
    < Content-Length: 7
    < 
    * Connection #0 to host 127.0.0.1 left intact
    WORKING

...

    wrk -t 10 -c 200 -d 30s --latency http://127.0.0.1:8080
    
    Running 30s test @ http://127.0.0.1:8080
      10 threads and 200 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     3.31ms    4.89ms  83.34ms   92.10%
        Req/Sec     7.76k     2.61k   14.31k    75.38%
      Latency Distribution
         50%    1.94ms
         75%    3.90ms
         90%    7.00ms
         99%   26.43ms
      2243497 requests in 30.00s, 184.00MB read
      Socket errors: connect 0, read 0, write 0, timeout 2
    Requests/sec:  74791.77
    Transfer/sec:      6.13MB
    
    wrk -t 10 -c 200 -d 30s --latency http://127.0.0.1:8080
    
    Running 30s test @ http://127.0.0.1:8080
      10 threads and 200 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     2.42ms    3.18ms 226.14ms   91.68%
        Req/Sec     9.19k     3.03k   34.22k    71.98%
      Latency Distribution
         50%    1.72ms
         75%    3.34ms
         90%    5.17ms
         99%   12.24ms
      2585570 requests in 29.99s, 212.06MB read
    Requests/sec:  86223.42
    Transfer/sec:      7.07MB
    
    wrk -t 10 -c 200 -d 30s --latency http://127.0.0.1:8080
    
    Running 30s test @ http://127.0.0.1:8080
      10 threads and 200 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     2.83ms    9.52ms 220.99ms   98.73%
        Req/Sec     9.13k     3.05k   36.89k    72.95%
      Latency Distribution
         50%    1.69ms
         75%    3.37ms
         90%    5.30ms
         99%   13.90ms
      2576093 requests in 29.99s, 211.28MB read
    Requests/sec:  85896.98
    Transfer/sec:      7.04MB


*HTTP Server Redis (proxy)*

(first, start Redis and set 'foo'='bar')
    
...

    curl -v http://127.0.0.1:8080/foo
    * About to connect() to 127.0.0.1 port 8080 (#0)
    *   Trying 127.0.0.1...
    * Adding handle: conn: 0x2416940
    * Adding handle: send: 0
    * Adding handle: recv: 0
    * Curl_addHandleToPipeline: length: 1
    * - Conn 0 (0x2416940) send_pipe: 1, recv_pipe: 0
    * Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
    > GET /foo HTTP/1.1
    > User-Agent: curl/7.32.0
    > Host: 127.0.0.1:8080
    > Accept: */*
    > 
    < HTTP/1.1 200 OK
    < Content-Type: text/plain; charset=utf-8
    < Content-Length: 3
    < 
    * Connection #0 to host 127.0.0.1 left intact
    bar

...

    wrk -t 10 -c 200 -d 30s --latency http://127.0.0.1:8080/foo
    
    Running 30s test @ http://127.0.0.1:8080/foo
      10 threads and 200 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     8.48ms    6.96ms  79.52ms   90.80%
        Req/Sec     2.86k   751.83     3.52k    87.19%
      Latency Distribution
         50%    6.39ms
         75%    7.56ms
         90%   14.27ms
         99%   40.46ms
      829683 requests in 30.00s, 64.88MB read
      Socket errors: connect 0, read 0, write 0, timeout 13
    Requests/sec:  27659.34
    Transfer/sec:      2.16MB
    
    wrk -t 10 -c 200 -d 30s --latency http://127.0.0.1:8080/foo
    
    Running 30s test @ http://127.0.0.1:8080/foo
      10 threads and 200 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     6.67ms    8.88ms 437.63ms   99.92%
        Req/Sec     3.19k   556.05    12.50k    85.62%
      Latency Distribution
         50%    6.32ms
         75%    7.19ms
         90%    8.21ms
         99%   11.05ms
      902766 requests in 30.00s, 70.60MB read
      Socket errors: connect 0, read 0, write 0, timeout 9
    Requests/sec:  30095.24
    Transfer/sec:      2.35MB
    
    wrk -t 10 -c 200 -d 30s --latency http://127.0.0.1:8080/foo
    
    Running 30s test @ http://127.0.0.1:8080/foo
      10 threads and 200 connections
      Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     6.56ms    2.50ms 228.52ms   93.31%
        Req/Sec     3.19k   529.67     8.78k    78.13%
      Latency Distribution
         50%    6.39ms
         75%    7.21ms
         90%    8.19ms
         99%   10.83ms
      907289 requests in 30.00s, 70.95MB read
    Requests/sec:  30246.49
    Transfer/sec:      2.37MB

...
