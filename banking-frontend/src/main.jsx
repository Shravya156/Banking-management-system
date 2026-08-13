import React from 'react'
import ReactDOM from 'react-dom/client' // Ensure the 'D' and 'M' are capital
import App from './App.jsx'
import './index.css'

// It must be ReactDOM.createRoot (Not reactdom)
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)