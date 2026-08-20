# MX Frontend

A simple, responsive web interface for interacting with the MX AI assistant.

## Features

- **User Authentication**: Register and login with email/password
- **Chat Interface**: Send messages and receive AI-generated responses
- **Responsive Design**: Works on desktop and mobile devices
- **Persistent Storage**: Remembers login token in localStorage
- **Real-time Updates**: Messages appear as they arrive

## Quick Start

### Option 1: Simple HTTP Server (Python)

```bash
cd d:\MX\frontend
python -m http.server 3001
```

Then open: http://localhost:3001

### Option 2: Using Node.js

```bash
cd d:\MX\frontend
npx http-server -p 3001
```

### Option 3: Using VS Code Live Server

1. Open `index.html` in VS Code
2. Right-click and select "Open with Live Server"

## Configuration

The frontend is configured to connect to `http://localhost:8080/api` by default.

To change the API URL, edit `index.html` and update:
```javascript
const API_URL = 'http://localhost:8080/api';
```

## API Endpoints Used

- `POST /api/auth/register` — Create new account
- `POST /api/auth/login` — Login and get JWT token
- `POST /api/chat` — Send message and get response

## Architecture

The frontend is a single-page application (SPA) with:

- **Authentication View**: Login/Register forms
- **Chat View**: Conversation interface
- **Local Storage**: Persists authentication token
- **No Build Process**: Pure HTML/CSS/JavaScript (runs directly in browser)

## Browser Support

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Any modern browser with ES6 support

## Deployment

For production deployment:

1. Copy `index.html` to a static file server (nginx, Apache, etc.)
2. Update `API_URL` to your production API endpoint
3. Enable CORS on your backend API
4. Consider adding SSL/TLS

### Docker Example

```dockerfile
FROM nginx:alpine
COPY index.html /usr/share/nginx/html/
EXPOSE 80
```

## Security Considerations

⚠️ **Development Only**: This frontend stores JWT tokens in localStorage. For production:

1. Use httpOnly cookies instead of localStorage
2. Implement CSRF protection
3. Add proper error handling and user feedback
4. Validate all user inputs
5. Use HTTPS only
6. Implement rate limiting on the frontend
7. Add proper error boundaries

## Styling

The interface uses:
- CSS Grid for layout
- CSS Flexbox for message organization
- CSS Animations for smooth transitions
- Gradient backgrounds with a purple theme
- Responsive design with media queries

Colors:
- Primary: `#667eea` → `#764ba2` (gradient)
- Background: `#fafafa`
- Text: `#333`
- Borders: `#e9ecef`

## Testing

### Test Account

Create an account using the registration form, or if default users exist:

```
Email: test@example.com
Password: testPassword123
```

### Manual Testing Checklist

- [ ] Registration form validates all fields
- [ ] Login with valid credentials works
- [ ] Login with invalid credentials shows error
- [ ] Chat messages send and display correctly
- [ ] Token persists across page refreshes
- [ ] Logout clears token and shows login form
- [ ] Responsive layout works on mobile
- [ ] Keyboard (Enter) works for sending messages

## Future Enhancements

- [ ] Conversation history sidebar
- [ ] Tool/function availability display
- [ ] Voice input support
- [ ] Dark mode toggle
- [ ] Emoji support
- [ ] File upload for context
- [ ] Export conversation as PDF
- [ ] Multi-language support
- [ ] Settings panel
- [ ] Conversation sharing

## Troubleshooting

### "Cannot connect to API"

1. Verify the API is running: `curl http://localhost:8080/health`
2. Check CORS is enabled on the backend
3. Verify API_URL in index.html matches your backend

### "Token invalid or expired"

1. Clear localStorage: Open console and run `localStorage.clear()`
2. Register/login again

### "Port already in use"

Use a different port:
```bash
python -m http.server 3002
```

And update accordingly.

---

For backend documentation, see [core-service README](../core-service/README.md)
