# Play Store Publish Approval — Setup Guide

## Overview

This workflow adds a Telegram approval gate before publishing to Google Play Store. When a new build is ready, a Telegram message is sent with **Approve** / **Reject** buttons. Publishing only proceeds on approval.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        GitHub Actions                               │
│                                                                     │
│  Tag Push ──► build-apk.yml                                         │
│                  │                                                   │
│                  ├── Build & Sign APK/AAB                            │
│                  ├── Upload artifact                                 │
│                  └── POST to n8n webhook ──────────────────────┐     │
│                                                                │     │
└────────────────────────────────────────────────────────────────│─────┘
                                                                 │
                                                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      n8n.munywele.co.ke                              │
│                                                                     │
│  Flow 1: Build Notification                                         │
│  ┌──────────────────┐    ┌──────────────┐    ┌──────────────────┐   │
│  │ Receive Build    │───►│ Respond OK   │───►│ Send Telegram    │   │
│  │ Notification     │    │ (200)        │    │ Message          │   │
│  └──────────────────┘    └──────────────┘    └────────┬─────────┘   │
│                                                       │             │
│                                                       ▼             │
│                                               ┌──────────────────┐   │
│                                               │ Store Context    │   │
│                                               └──────────────────┘   │
│                                                                     │
│  Flow 2: Callback Handler                                           │
│  ┌──────────────────┐    ┌──────────────┐    ┌──────────────────┐   │
│  │ Wait for Callback│───►│ Lookup       │───►│ Has Context?     │   │
│  │ (Telegram Trigger│    │ Context      │    │ (IF node)        │   │
│  └──────────────────┘    └──────────────┘    └────────┬─────────┘   │
│                                                       │             │
│                              ┌─────────────────────────┤             │
│                              ▼                         ▼             │
│                    ┌──────────────────┐    ┌──────────────────┐      │
│                    │ Trigger Publish  │    │ Update Message   │      │
│                    │ Workflow         │    │ (Rejected)       │      │
│                    └────────┬─────────┘    └──────────────────┘      │
│                             ▼                                        │
│                    ┌──────────────────┐                              │
│                    │ Update Message   │                              │
│                    │ (Approved)       │                              │
│                    └──────────────────┘                              │
└─────────────────────────────────────────────────────────────────────┘
                                                                 │
                                                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        GitHub Actions                               │
│                                                                     │
│  publish.yml (workflow_dispatch)                                    │
│                  │                                                   │
│                  ├── Download artifact                               │
│                  ├── Upload to Play Store                            │
│                  ├── Upload to GitHub Release                        │
│                  └── Send webhook notification                       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Flow Breakdown

### Flow 1: Build Notification

| Step | Node | Purpose |
|------|------|---------|
| 1 | **Receive Build Notification** | Webhook receives POST from GitHub Actions with build info |
| 2 | **Respond OK** | Returns `200 OK` immediately so GitHub doesn't timeout |
| 3 | **Send Telegram Message** | Sends formatted message with Approve/Reject inline buttons |
| 4 | **Store Context** | Extracts and stores build info (run_id, tag, track, etc.) for reference |

### Flow 2: Callback Handler

| Step | Node | Purpose |
|------|------|---------|
| 1 | **Wait for Callback** | Telegram Trigger polls for inline button taps (`callback_query`) |
| 2 | **Parse Callback** | Extracts action, run_id, track, tag from callback_data |
| 3 | **Is Approve?** | Routes based on action (`approve` or `reject`) |
| 4 | **Trigger Publish Workflow** | POSTs to GitHub API to trigger `publish.yml` |
| 5 | **Update Message (Approved/Rejected)** | Edits the original Telegram message to show result |

## Data Flow

### How chat_id Flows

```
GitHub Variable                GitHub Actions              n8n Webhook Payload
──────────────                 ──────────────              ───────────────────
TELEGRAM_CHAT_ID ──────► build-apk.yml reads it ──────► { "chat_id": "-100..." }
("-1001234567890")          via ${{ vars. }}                 │
                                                            │
                              ┌──────────────────────────────┘
                              ▼
                      Flow 1: Send Telegram Message
                      Uses $json.body.chat_id
                              │
                              ▼
                      Message sent with inline keyboard
```

### How Static Data Bridges Flows

**No static data needed.** All required data is encoded in the callback_data itself:

```
approve:RUN_ID:TRACK:TAG
reject:RUN_ID:TRACK:TAG
```

Example:
```
approve:12345678:beta:v1.2.3-beta
```

The Parse Callback node splits this string to extract all values. No state is stored between flows.

**Callback data limit:** 64 bytes. Format fits within this limit for typical tags.

### Webhook Payload (GitHub → n8n)

```json
{
  "event": "playstore_publish_approval",
  "repo": "IITA-AKILIMO/akilimo-mobile",
  "tag": "v1.2.3-beta",
  "version": "1234",
  "track": "beta",
  "actor": "masgeek",
  "run_id": "12345678",
  "run_url": "https://github.com/IITA-AKILIMO/akilimo-mobile/actions/runs/12345678",
  "artifact_name": "app-release",
  "chat_id": "-1001234567890",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

| Field | Source | Used By |
|-------|--------|---------|
| `chat_id` | `${{ vars.TELEGRAM_CHAT_ID }}` | Send Telegram Message → `$json.body.chat_id` |
| `tag` | `${{ github.ref_name }}` | Callback data, GitHub dispatch |
| `run_id` | `${{ github.run_id }}` | Callback data, uniquely identifies this build |
| `track` | Computed from tag | Determines beta vs production |
| `repo` | `${{ github.repository }}` | GitHub dispatch URL |

### Reply Text (Telegram → n8n)

The user taps an inline keyboard button. The callback data encodes the action and run_id:

```
approve:RUN_ID
reject:RUN_ID
```

The IF node checks `$json.action` equals `approve` or `reject`.

### GitHub Dispatch (n8n → GitHub)

```json
{
  "ref": "v1.2.3-beta",
  "inputs": {
    "version": "v1.2.3-beta",
    "artifact_name": "app-release",
    "track": "beta",
    "approved_by": "masgeek"
  }
}
```

## Prerequisites

### 1. Create Telegram Bot

1. Open Telegram, search for `@BotFather`
2. Send `/newbot`
3. Follow prompts to name your bot
4. Copy the bot token (format: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

### 2. Get Chat ID

**For personal chat:**
1. Search for `@getmyid_bot` in Telegram
2. Send `/getid`
3. Copy your chat ID

**For group chat:**
1. Add `@getmyid_bot` to the group
2. Send `/getid` in the group
3. Copy the group chat ID (will be negative, e.g., `-1001234567890`)

### 3. Get GitHub Personal Access Token

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens
2. Click **Generate new token**
3. Name it `n8n-workflow-trigger`
4. Set repository access to `IITA-AKILIMO/akilimo-mobile`
5. Under **Permissions → Actions**, select **Read and write**
6. Generate and copy the token

## Setup Steps

### Step 1: GitHub Secrets and Variables

**Secrets** (Settings → Secrets and variables → Actions → Secrets):

| Secret | Value | Description |
|--------|-------|-------------|
| `TELEGRAM_BOT_TOKEN` | `123456789:ABCdef...` | Bot token from @BotFather |
| `N8N_WEBHOOK_SECRET` | `your-secret-string` | Shared secret (optional, for HMAC verification) |

**Variables** (Settings → Secrets and variables → Actions → Variables):

| Variable | Value | Description |
|----------|-------|-------------|
| `TELEGRAM_CHAT_ID` | `-1001234567890` | Chat/group ID — passed to n8n in webhook payload |
| `N8N_WEBHOOK_URL` | `https://n8n.munywele.co.ke/webhook/playstore-approval` | n8n webhook endpoint |

### Step 2: n8n Credentials

Create these credentials in n8n (Settings → Credentials → Add):

**1. Telegram Bot API**
- Name: `Telegram Bot`
- Type: `Telegram API`
- Access Token: Your bot token

**2. GitHub Token (HTTP Header Auth)**
- Name: `GitHub Token`
- Type: `Header Auth`
- Header Name: `Authorization`
- Header Value: `Bearer ghp_yourGitHubToken`

### Step 3: n8n Environment Variables

**Not required.** The chat ID is passed from GitHub Actions in the webhook payload, so n8n doesn't need to access environment variables.

### Step 4: Import Workflow

1. Open n8n at `https://n8n.munywele.co.ke`
2. Click **Add workflow** → **Import from File**
3. Select `.n8n/playstore-approval.json`
4. Update credentials:
   - Double-click each **Telegram** node → select `Telegram Bot` credential
   - Double-click **Trigger Publish Workflow** → select `GitHub Token` credential
5. Update the GitHub repo URL in **Trigger Publish Workflow** node
6. Click **Save**
7. Click **Active** toggle to enable

### Step 5: Activate Webhook

1. In the workflow, click the **Receive Build Notification** node
2. Copy the **Test URL** or **Production URL**
3. Update `N8N_WEBHOOK_SECRET` in GitHub if using HMAC

## Testing

### Test Webhook

```bash
curl -X POST https://n8n.munywele.co.ke/webhook-test/playstore-approval \
  -H "Content-Type: application/json" \
  -d '{
    "event": "playstore_publish_approval",
    "repo": "IITA-AKILIMO/akilimo-mobile",
    "tag": "v1.0.0-test",
    "version": "999",
    "track": "beta",
    "actor": "test-user",
    "run_id": "12345",
    "run_url": "https://github.com/IITA-AKILIMO/akilimo-mobile/actions/runs/12345",
    "artifact_name": "app-release",
    "chat_id": "-1001234567890",
    "timestamp": "2025-01-15T10:00:00Z"
  }'
```

### Test Full Flow

1. Push a tag to trigger `build-apk.yml`:
   ```bash
   git tag v1.0.0-test
   git push origin v1.0.0-test
   ```
2. Check GitHub Actions — build should complete
3. Check Telegram — approval message should appear
4. Tap **Approve** button
5. Check n8n — callback should be received
6. Check GitHub Actions — `publish.yml` should be triggered
7. Check Telegram — message should update to "Approved"

## Telegram Message Preview

**Bot sends:**
```
🚀 Play Store Publish Request

Tag: v1.2.3-beta
Version: 1234
Track: beta
Triggered by: @masgeek
Run: View Build

[✅ Approve]  [❌ Reject]
```

**After approval:**
```
✅ Approved

Published to beta by @masgeek
```

**After rejection:**
```
❌ Rejected

Publish rejected by @masgeek
```

## Troubleshooting

### Webhook not receiving data

1. Check n8n workflow is **Active**
2. Verify the webhook URL is correct
3. Check GitHub Actions logs for the POST request
4. Check n8n execution history for errors

### Telegram message not sent

1. Verify bot token in n8n credentials
2. Check that `TELEGRAM_CHAT_ID` is set as a GitHub Actions variable
3. Verify the webhook payload includes `chat_id` (check n8n execution data)
4. Ensure bot is added to the group (if using group chat)
5. Send a test message to verify bot is working

### Callback not received

1. Ensure Telegram Trigger node is active
2. Check n8n execution history
3. Verify bot has permission to receive callbacks
4. User must tap the button within 48 hours (Telegram limitation)
5. Check that callback_data format is correct (`approve:RUN_ID:TRACK:TAG`)

### Callback data parsing fails

1. Verify callback_data contains exactly 4 parts separated by `:`
2. Check that track and tag don't contain `:` characters
3. If tag contains `:`, only the first part after track will be used

### Publish workflow not triggered

1. Check GitHub token has `actions:write` permission
2. Verify the repo name in the HTTP Request node
3. Check n8n execution history for the HTTP Request node
4. Verify the workflow file `publish.yml` exists in `.github/workflows/`

## Security Notes

- The webhook does not verify HMAC by default. To enable, add a Code node after the webhook to verify the signature.
- Use fine-grained GitHub tokens with minimal permissions.
- Store all secrets in n8n's credential manager, not in the workflow.
- The Telegram callback data contains the run_id — ensure only authorized users can access the chat.

## Customization

### Change Timeout Behavior

The Telegram Trigger polls every minute. To change this, edit the **Wait for Callback** node:
- `everyMinute` → default
- `everyFiveMinutes` → less frequent polling
- `custom` → set specific interval

### Add More Approvers

Modify the callback data to include an approver list:
1. Store allowed user IDs in the webhook payload
2. Add an IF node after **Wait for Callback** to check `callback_query.from.id`

### Add Expiry

Add a Timeout node or use n8n's built-in timeout to auto-reject after a set time.
