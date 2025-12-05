<script setup>
import AuthLayout from '@/components/layout/AuthLayout.vue'
import AuthCard from '@/components/auth/AuthCard.vue'
import RegisterForm from '@/components/auth/RegisterForm.vue'
import AuthFooter from '@/components/auth/AuthFooter.vue'
</script>

<template>
  <AuthLayout>
    <AuthCard
      title="Create an account"
      subtitle="Join our platform"
    >
      <RegisterForm />

      <template #footer>
        <AuthFooter
          text="Already have an account?"
          linkText="Sign in"
          to="/login"
        />
      </template>

    </AuthCard>
  </AuthLayout>
</template>
