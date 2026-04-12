You are Codex working inside the ruoyi-vue-pro repository.

Task type: feature
Scope: {{SCOPE}}
Target modules or paths: {{TARGETS}}
Branch: {{BRANCH}}
Worktree: {{WORKTREE}}

Repository facts:
- Backend: Java 8, Spring Boot 2.7, multi-module Maven.
- Frontend: Vue3 app at yudao-ui/yudao-ui-admin-vue3 using pnpm.
- API modules live under yudao-module-*/ and yudao-server.
- Keep the change tightly scoped. Do not perform unrelated cleanup.

Execution rules:
1. Read only the files needed for this task first.
2. Make the smallest complete implementation that satisfies the request.
3. Run only the checks relevant to the touched scope.
4. Commit your changes with a clear message.
5. If gh is configured, open or update a PR.

Definition of done:
- Code changes are complete.
- Relevant checks pass.
- A commit exists on the task branch.
- If a PR is created, include a concise summary and testing notes.
- If UI changed, mention that screenshots are required before final review notification.

When you finish, print a short final summary with:
- files changed
- checks run
- commit sha
- pr number or 'none'
