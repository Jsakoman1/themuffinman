import axios from "axios"
import {authHeader} from "../auth.ts"

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"

export const api = axios.create({
  baseURL: API_BASE_URL
})

export const withAuth = () => ({
  headers: authHeader()
})
