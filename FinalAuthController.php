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
     * Register a new user via API (simplified - no approval required)
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

        // Create API token for immediate login
        try {
            $token = $user->createToken('mobile-app')->plainTextToken;

            return response()->json([
                'message' => 'Registration successful! You can now use the app.',
                'user' => $user,
                'token' => $token,
                'success' => true
            ], 201);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Registration successful but token creation failed. Please try logging in.',
                'user' => $user,
                'success' => false,
                'error' => $e->getMessage()
            ], 201);
        }
    }

    /**
     * Login user via API (simplified - no approval check)
     */
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $user = User::where('email', $request->email)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'message' => 'The provided credentials are incorrect.',
                'success' => false
            ], 401);
        }

        // Create API token
        try {
            // Delete old tokens for this device/app
            $user->tokens()->where('name', 'mobile-app')->delete();

            // Create new token
            $token = $user->createToken('mobile-app')->plainTextToken;

            return response()->json([
                'message' => 'Login successful',
                'user' => $user,
                'token' => $token,
                'success' => true
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Login successful but API token creation failed',
                'user' => $user,
                'success' => false,
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
            // Delete current access token
            $request->user()->currentAccessToken()->delete();

            return response()->json([
                'message' => 'Logged out successfully',
                'success' => true
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'message' => 'Logout completed',
                'success' => true
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
            'message' => 'Profile retrieved successfully',
            'success' => true
        ]);
    }
}
