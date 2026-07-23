# frozen_string_literal: true

root = File.expand_path("../..", __dir__)
read = ->(path) { File.read(File.join(root, path)) }
failures = []

surface_row = read.call("apps/themuffinman/frontend/src/modules/app-shell/components/SurfaceRow.vue")
work = read.call("apps/themuffinman/frontend/src/modules/app-shell/views/WorkDiscoveryView.vue")
applications = read.call("apps/themuffinman/frontend/src/modules/app-shell/views/WorkApplicationsView.vue")
chat_routes = read.call("apps/themuffinman/frontend/src/modules/app-shell/shellRouteRegistry.ts")
create_menu = read.call("apps/themuffinman/frontend/src/modules/app-shell/components/UniversalCreateMenu.vue")
search = read.call("apps/themuffinman/frontend/src/modules/app-shell/components/GlobalSearchEntry.vue")

failures << "SurfaceRow lacks aria-selected" unless surface_row.include?(":aria-selected=")
failures << "SurfaceRow lacks keyboard open handling" unless surface_row.include?("@keydown.enter") && surface_row.include?("@keydown.space")
failures << "My work does not select MY_VISIBLE" unless work.include?("MY_VISIBLE")
failures << "Find work does not select AVAILABLE" unless work.include?("AVAILABLE")
failures << "My applications does not use getMyApplications" unless applications.include?("getMyApplications")
failures << "Chat has duplicate /chat route registry entries" if chat_routes.scan(/pathPattern: \/\^\\\/chat\$\//).length > 1
failures << "Create popover lacks fixed positioning" unless create_menu.include?("position:fixed")
failures << "Create popover lacks viewport start/end constraints" unless create_menu.include?("inset-inline-start") && create_menu.include?("inset-inline-end")
failures << "Search popover lacks fixed positioning" unless search.include?("position:fixed")
failures << "Search popover lacks viewport start/end constraints" unless search.include?("inset-inline-start") && search.include?("inset-inline-end")

abort "Frontend interaction contract audit failed:\n- #{failures.join("\n- ")}" unless failures.empty?
puts "Frontend interaction contract audit passed (row, scope, application, chat, and popover contracts)."
