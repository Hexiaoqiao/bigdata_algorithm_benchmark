# Bigdata Algorithm Benchmark
Some benchmarks about basic data structures for bigdata.

## Hardware:
Intel(R) Xeon(R) E5-2650 CPU 8cores 32processors @ 2.60GHz (CentOS release 6.6 x86_64)

## Version:
java version "1.7.0_76" Java HotSpot(TM) 64-Bit Server VM (build 24.76-b04, mixed mode)

## Dictionary
|TestSet| Data Structure| Memory Footprint (byte) | Build Time(ms) | Retrieval Time(ms) |
|:--|:--| ----: | -----:| -----:| ---: |
|endict 200K|DAT      | 536384  |  2| 58  |
|endict 200K|HashMap  | 11686512| 65| 89  |
|endict 200K|TrieDict | 31168584|575| 765 |
|endict 200K|RadixTree| 21255768|754| 1967|
|cndict 200K|DAT      | 536384  |  1| 43  |
|cndict 200K|HashMap  | 11667376| 43| 73  |
|cndict 200K|TrieDict | 29723528|633| 704 |
|cndict 200K|RadixTree| 16025264|9373| 20948|

## How to Contribute
A. Create directory for new Data Structure.  
B. Include the classic implements, open source is best.  
C. Add new Benchmark code for differet implements.  
D. Mark sure to upload testset if need be.
E. Update README with the test result following #Dictionary.