# Frontend State Logic Duplication Audit

- Decision: `review`
- Why: active files=16
- Next action: review the overlap groups below
- Evidence: mutation=0, workflow=3, dialog=0, feedback=3

## `workflow_action_overlap`

- `blockUser` files=`2`
- `createCircle` files=`2`
- `unblockUser` files=`2`

## `feedback_error_overlap`

- `error_assignment` files=`6`
- `getApiErrorMessage` files=`5`
- `showMessage` files=`4`
