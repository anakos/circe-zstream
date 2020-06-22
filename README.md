circe-zstream
-------------

This is a port of circe-fs2, replacing fs2 dependency with zstream.  Method names from the upstream project have been preserved here and the test suite has - for the most part - been, likewise, reproduced.

# raison d'etre

This project came to being as I was getting started with the zio ecosystem and wanted to get a better idea of how to work with  zstream.  More specifically, I was looking to connect the [sttp async http client backend](https://sttp.softwaremill.com/en/latest/backends/zio.html) with a streaming decoder.
