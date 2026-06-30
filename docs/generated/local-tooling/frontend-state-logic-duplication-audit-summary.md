# Frontend State Logic Duplication Audit

- Decision: `review`
- Why: active files=34
- Next action: review the overlap groups below
- Evidence: mutation=2, workflow=9, dialog=5, feedback=3

## `workflow_action_overlap`

- `approveApplication` files=`4`
- `declineApplication` files=`4`
- `applyForQuest` files=`2`
- `blockUser` files=`2`
- `confirmQuestTermChange` files=`2`

## `feedback_error_overlap`

- `getApiErrorMessage` files=`12`
- `error_assignment` files=`9`
- `showFeedback` files=`4`
