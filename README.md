# instant
Make JSR310 available from terminal

Usage
===

```shell session
$ instant
2006-01-02T15:04:05.600Z

$ instant -f unix
1136214245000

$ instant -d 2020-01-02
2020-01-02T00:00:00Z

$ instant -d 2020-01-02 -f unix
1577923200000

$ instant -t 03:04:05
2006-01-02T03:04:05Z

$ instant -f "uuuu/MM/dd hh:mm:ssZ"
2006/01/02 15:04:05+00:00

$ instant -a PT30M
2006-01-02T15:34:05.600Z
```
