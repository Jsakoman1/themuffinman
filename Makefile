SHELL := /bin/zsh

.PHONY: dev backend-dev backend-test backend-package backend-bootstrap-example audit-agent-safety bootstrap-feature-work feature-closeout-audit

dev:
	$(MAKE) -C apps/themuffinman dev

backend-dev:
	$(MAKE) -C apps/themuffinman backend-dev

backend-test:
	$(MAKE) -C apps/themuffinman backend-test

backend-package:
	$(MAKE) -C apps/themuffinman backend-package

backend-bootstrap-example:
	$(MAKE) -C apps/themuffinman backend-bootstrap-example

audit-agent-safety:
	$(MAKE) -C apps/themuffinman audit-agent-safety

bootstrap-feature-work:
	@if [ -z "$(topic)" ]; then echo "usage: make bootstrap-feature-work topic=<short-feature-topic> [risk=<tier>]"; exit 1; fi
	/bin/zsh scripts/bootstrap-feature-work.sh "$(topic)" "$(if $(risk),$(risk),medium)"

feature-closeout-audit:
	@if [ -z "$(manifest)" ]; then echo "usage: make feature-closeout-audit manifest=<manifest-file>"; exit 1; fi
	/bin/zsh scripts/feature-closeout-audit.sh "$(manifest)"
