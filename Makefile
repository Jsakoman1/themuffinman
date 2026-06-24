SHELL := /bin/zsh

.PHONY: dev backend-dev backend-test backend-package backend-bootstrap-example

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
