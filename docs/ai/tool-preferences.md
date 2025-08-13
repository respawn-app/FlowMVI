## Primary Tools

- `Edit`/`MultiEdit` - PREFER.
- `mcp__jetbrains__replace_specific_text` - AVOID. Use Update() instead
- `mcp__jetbrains__create_new_file_with_text` - AVOID. Duplicated by Write.

- `mcp__jetbrains__search_in_files_content` - Prefer to bash commands. Fuzzy search across project files.
- `mcp__jetbrains__get_current_file_errors`, `mcp__jetbrains__get_project_problems` - Use to get compilation errors without a rebuild
- `mcp__jetbrains__list_files_in_folder` - Prefer for project directory listing, less context overload.
- `mcp__jetbrains__list_directory_tree_in_folder` - Prefer for hierarchical project structure, less overload.
- `mcp__jetbrains__get_project_dependencies` - Dependency analysis
- `mcp__jetbrains__get_run_configurations` - Available run configurations, allows you to start the app & test.
- `mcp__jetbrains__run_configuration` - Run or test the app, essential for user feedback.
- `mcp__jetbrains__get_open_in_editor_file_text` - Avoid. Duplicated by your IDE tools.

- `Bash` - All command execution.
- `Read` - Prefer over MCP. Always use before Edit()
- `Grep` - Search beyond project boundaries
- `Glob` - File pattern matching across filesystem
- `LS` - When absolute paths needed, otherwise use mcp
- `Task` - Complex multi-step operations. Essential for work parallelization.
