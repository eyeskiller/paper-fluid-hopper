# Fluid Hopper Changelog

## Version 1.0.2
- Integrated Plugin Analytics API for asynchronous usage tracking.

## Version 1.0.1
- Fixed chunk thrashing by checking if chunks are loaded before checking hopper state.
- Fixed `ConcurrentModificationException` risk by using `ConcurrentHashMap.newKeySet()` for stored locations.
- Synchronized file writing to prevent `hoppers.yml` data corruption.
- Batched file saves at the end of tick cycles instead of continuously triggering them.
