An Android application fetching locations from [where-server](https://github.com/wujek-srujek/where-server)
and displaying them with Google Maps API. Intended usage is to provide location information during our sabbatical leave.

TODO:
- Use Google OAuth 2 (get token on the device, send with each request, validate on the server and fetch basic user info,
  base authorization on rules matching user name, email or whatever).
- Use Geocoder on a worker thread.
- Get rid of callback hell, use reactive programming.
- Rewrite in Kotlin, just to get some working experience with it.
