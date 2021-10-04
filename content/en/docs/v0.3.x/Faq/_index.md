
---
title: "FAQ"
linkTitle: "FAQ"
weight: 6
---

## Generated String value is weired. Help!
Generated String is ASCII code consists of unprintable `control block`, printable characters.
If you want to generate only printable characters as default, You should override `AnnotatedArbitraryGenerator`.
check [Example]({{< relref "/docs/v0.3.x/examples/annotatedarbitrarygenerator#override-string-to-generate-printable-characters" >}})

## Generate object fails. Help!

Input class is not suitable for given `ArbitraryGenerator`, check out [ArbitraryGenerator]({{< relref "/docs/v0.3.x/features/arbitrarygenerator">}}).
If Error occurs again, please create an issue.
