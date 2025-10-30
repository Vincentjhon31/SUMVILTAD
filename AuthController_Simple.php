<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;
use Illuminate\Validation\Rules;

class AuthController extends Controller
{
    /**
     * Register a new user via API (matches web registration)
     */
    public function register(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|lowercase|email|max:255|unique:'.User::class,
            'password' => ['required', 'confirmed', Rules\Password::defaults()],
            'role' => 'required|in:admin,farmer',
        ]);

        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'role' => $request->role,
        ]);

        // Create API token for immediate login (like web version)
        try {
            $token = $user->createToken('mobile-app')->plainTextToken;

            return response()->json([
                'message' => 'Registration successful! You can now use the app.',
                'user' => $user,
                'token' => $token
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Registration successful but token creation failed. Please try logging in.',
                'user' => $user,
                'error' => $e->getMessage()
            ], 201);
        }
    }

    /**
     * Login user via API (simple, no approval needed)
     */
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $user = User::where('email', $request->email)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['The provided credentials are incorrect.'],
            ]);
        }

        // Simple login - no approval checks needed (like your web version)
        try {
            // Delete old tokens for this device/app
            $user->tokens()->where('name', 'mobile-app')->delete();

            // Create new token
            $token = $user->createToken('mobile-app')->plainTextToken;

            return response()->json([
                'message' => 'Login successful',
                'user' => $user,
                'token' => $token
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Login successful but API token creation failed',
                'user' => $user,
                'error' => $e->getMessage()
            ], 200);
        }
    }

    /**
     * Logout user via API
     */
    public function logout(Request $request)
    {
        try {
            $request->user()->currentAccessToken()->delete();
            return response()->json([
                'message' => 'Logged out successfully'
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Logout completed'
            ]);
        }
    }

    /**
     * Get authenticated user profile
     */
    public function profile(Request $request)
    {
        $user = $request->user();

        return response()->json([
            'user' => $user,
            'message' => 'Profile retrieved successfully'
        ]);
    }
}
