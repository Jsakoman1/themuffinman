# frozen_string_literal: true

root = File.expand_path("../..", __dir__)
read = ->(path) { File.read(File.join(root, path)) }
failures = []

surface_row = read.call("apps/themuffinman/frontend/src/modules/app-shell/components/SurfaceRow.vue")
work = read.call("apps/themuffinman/frontend/src/modules/app-shell/views/WorkDiscoveryView.vue")
applications = read.call("apps/themuffinman/frontend/src/modules/app-shell/views/WorkApplicationsView.vue")
chat_routes = read.call("apps/themuffinman/frontend/src/modules/app-shell/shellRouteRegistry.ts")
action_dialog = read.call("apps/themuffinman/frontend/src/modules/app-shell/components/AppActionDialog.vue")
search_field = read.call("apps/themuffinman/frontend/src/modules/app-shell/components/AppSearchField.vue")

failures << "SurfaceRow lacks aria-selected" unless surface_row.include?(":aria-selected=")
failures << "SurfaceRow lacks keyboard open handling" unless surface_row.include?("@keydown.enter") && surface_row.include?("@keydown.space")
failures << "My work does not select MY_VISIBLE" unless work.include?("MY_VISIBLE")
failures << "Find work does not select AVAILABLE" unless work.include?("AVAILABLE")
failures << "My applications does not use getMyApplications" unless applications.include?("getMyApplications")
failures << "Chat has duplicate /chat route registry entries" if chat_routes.scan(/pathPattern: \/\^\\\/chat\$\//).length > 1
failures << "Shared action dialog lacks modal semantics" unless action_dialog.include?("role=\"dialog\"") && action_dialog.include?("aria-modal=\"true\"")
failures << "Shared action dialog lacks body teleport" unless action_dialog.include?("<Teleport to=\"body\">")
failures << "Shared search field lacks search semantics" unless search_field.include?("role=\"search\"") && search_field.include?("type=\"search\"")

abort "Frontend interaction contract audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Frontend interaction contract audit passed (row, scope, application, chat, dialog, and search contracts)."
